import Collections.QuadTree;
import models.Point;
import models.Rectangle;

import java.util.ArrayList;

import static java.lang.Math.ceil;

// Concrete generation strategy for 2pos
class Strategy2pos extends GenerationStrategy {
    @Override
    Point[] generate() {

        int counter = 0;
        double width = data.result * data.ratio;

        generateStart2();

        while (rectangles.size() < data.n && counter < data.n * 1e5) {
            counter++;

            Point candidate = new Point(data.xGenerator.sample(0, 10000), data.yGenerator.sample(0, 10000));
            while (pointsTree.query2D(new Rectangle(candidate.getX() - 0.5, candidate.getY() - 0.5, candidate.getX() + 0.5, candidate.getY() + 0.5)).size() > 0) {
                candidate = new Point(data.xGenerator.sample((int) ceil(width), (int) (10000 - ceil(width))), data.yGenerator.sample((int) ceil(width), (int) (10000 - ceil(width))));
            }
            boolean useLeft = rand.nextBoolean();

            Rectangle candidateRectangle;
            if (useLeft) {
                candidateRectangle = new Rectangle(candidate.getX() - width, candidate.getY(), candidate.getX(), candidate.getY() + height, candidate);
            } else {
                candidateRectangle = new Rectangle(candidate.getX(), candidate.getY(), candidate.getX() + width, candidate.getY() + height, candidate);
            }

            if (tree.query2D(candidateRectangle).size() == 0) {
                rectangles.add(candidateRectangle);
                tree.insert(candidateRectangle);
                pointsTree.insert(candidate);
            }
        }

        Point[] associatedPoints = new Point[rectangles.size()];
        for (int i = 0; i < rectangles.size(); i++) {
            associatedPoints[i] = (Point) rectangles.get(i).getPoI();
        }

        return associatedPoints;
    }

    void generateStart2() {
        // Constructing initial point and rectangles
        Point startPoint = new Point(data.xGenerator.sample(3 * (int) ceil(width), (int) (10000 - 3 * ceil(width))), data.yGenerator.sample((int) ceil(height), (int) (10000 - ceil(height))));
        Rectangle rightRectangle = new Rectangle(startPoint.getX(), startPoint.getY(), startPoint.getX() + width, startPoint.getY() + height, startPoint);
        Rectangle leftRectangle = new Rectangle(startPoint.getX() - width, startPoint.getY(), startPoint.getX(), startPoint.getY() + height, startPoint);

        // invalidate left rectangle
        Point[] internalLeft = leftRectangle.getInternal();
        int randIndex = rand.nextInt(internalLeft.length);
        Point invalidator = internalLeft[randIndex];
        // create corresponding rectangle invalidator
        Rectangle invalidatorRectangle = new Rectangle(invalidator.getX() - width, invalidator.getY(), invalidator.getX(), invalidator.getY() + height, invalidator);

        // lock in right rectangle
        Point[] boundaryRight = rightRectangle.getBoundaryStrict(true, true, false, false);
        randIndex = rand.nextInt(boundaryRight.length);
        Point blocker = boundaryRight[randIndex];
        // create corresponding rectangle blocker
        Rectangle blockerRectangle = new Rectangle(blocker.getX(), blocker.getY(), blocker.getX() + width, blocker.getY() + height, blocker);

        // Add constructed rectangles to rectangles
        rectangles.add(rightRectangle);
        rectangles.add(invalidatorRectangle);
        rectangles.add(blockerRectangle);

        // Add rectangles to tree
        tree.insert(rightRectangle);
        tree.insert(invalidatorRectangle);
        tree.insert(blockerRectangle);

        // Add points to pointsTree
        pointsTree.insert(startPoint);
        pointsTree.insert(invalidator);
        pointsTree.insert(blocker);
    }

    @Override
    Rectangle[] generateStart() {
        // ArrayList storing rectangles to be returned
        ArrayList<Rectangle> returnRectangles = new ArrayList<>();

        // initial point
        Point startPoint = new Point(data.xGenerator.sample(3 * (int) ceil(width), (int) (10000 - 3 * ceil(width))), data.yGenerator.sample((int) ceil(width), (int) (10000 - ceil(width))));
        // Possible labels for initial point
        Rectangle leftRectangle = new Rectangle(new Point(startPoint.getX() - width, startPoint.getY()), new Point(startPoint.getX(), startPoint.getY() + height), startPoint);
        Rectangle rightRectangle = new Rectangle(new Point(startPoint.getX(), startPoint.getY()), new Point(startPoint.getX() + width, startPoint.getY() + height), startPoint);

        boolean useLeft = rand.nextBoolean();
        if (useLeft) {
            returnRectangles.add(leftRectangle);

            // Constructing rectangle making right option invalid
            Point[] internalRight = rightRectangle.getInternal();
            int randIndex = rand.nextInt(internalRight.length);
            Point invalidator = internalRight[randIndex];
            returnRectangles.add(new Rectangle(invalidator, new Point(invalidator.getX() + width, invalidator.getY() + height), invalidator));

            // Constructing rectangle limiting size of starting rectangle
            Point[] boundaryLeft = leftRectangle.getBoundary(true,false, false, true);
            if (boundaryLeft.length == 0) {
                // must lock in with two rectangles
                Point lockPoint = new Point(startPoint.getX() - 2 * width, startPoint.getY());
                returnRectangles.add(new Rectangle(lockPoint, new Point(startPoint.getX() - width, startPoint.getY() + height), lockPoint));

                // Construct final blocker
                Point[] finalBlockerOptions = (new Rectangle(new Point(lockPoint.getX() - width, lockPoint.getY()), new Point(lockPoint.getX(), lockPoint.getY() + height), lockPoint)).getInternal();
                randIndex = rand.nextInt(finalBlockerOptions.length);
                Point finalBlocker = finalBlockerOptions[randIndex];
                returnRectangles.add(new Rectangle(new Point(finalBlocker.getX() - width, finalBlocker.getY()), new Point(finalBlocker.getX(), finalBlocker.getY() + height), finalBlocker));
            } else {
                randIndex = rand.nextInt(boundaryLeft.length);
                Point blocker = boundaryLeft[randIndex];
                while (blocker.getX() == startPoint.getX()) {
                    randIndex = rand.nextInt(boundaryLeft.length);
                    blocker = boundaryLeft[randIndex];
                }

                if (blocker.getY() < leftRectangle.getTopRight().getY()) {
                    returnRectangles.add(new Rectangle(new Point(blocker.getX() - width, blocker.getY()), new Point(blocker.getX(), blocker.getY() + height), blocker));
                } else {
                    useLeft = rand.nextBoolean();
                    if (useLeft) {
                        returnRectangles.add(new Rectangle(new Point(blocker.getX() - width, blocker.getY()), new Point(blocker.getX(), blocker.getY() + height), blocker));
                    } else {
                        returnRectangles.add(new Rectangle(new Point(blocker.getX(), blocker.getY()), new Point(blocker.getX() + width, blocker.getY() + height), blocker));
                    }
                }
            }
        } else {
            returnRectangles.add(rightRectangle);

            // Constructing rectangle making left option invalid
            Point[] internalLeft = leftRectangle.getInternal();
            int randIndex = rand.nextInt(internalLeft.length);
            Point invalidator = internalLeft[randIndex];
            returnRectangles.add(new Rectangle(new Point(invalidator.getX() -width, invalidator.getY()), new Point(invalidator.getX(), invalidator.getY() + height), invalidator));

            // Constructing rectangle limiting size of starting rectangle
            Point[] boundaryRight = rightRectangle.getBoundary(true,true, false, false);
            if (boundaryRight.length == 0) {
                // must lock in with two rectangles
                Point lockPoint = new Point(startPoint.getX() + 2 * width, startPoint.getY());
                returnRectangles.add(new Rectangle(new Point(startPoint.getX() + width, startPoint.getY()), new Point(startPoint.getX() + 2 * width, startPoint.getY() + height), lockPoint));

                // Construct final blocker
                Point[] finalBlockerOptions = (new Rectangle(lockPoint, new Point(lockPoint.getX() + width, lockPoint.getY() + height), lockPoint)).getInternal();
                randIndex = rand.nextInt(finalBlockerOptions.length);
                Point finalBlocker = finalBlockerOptions[randIndex];
                returnRectangles.add(new Rectangle(finalBlocker, new Point(finalBlocker.getX() + width, finalBlocker.getY() + height), finalBlocker));
            } else {
                randIndex = rand.nextInt(boundaryRight.length);
                Point blocker = boundaryRight[randIndex];
                while (blocker.getX() == startPoint.getX()) {
                    randIndex = rand.nextInt(boundaryRight.length);
                    blocker = boundaryRight[randIndex];
                }

                if (blocker.getY() < leftRectangle.getTopRight().getY()) {
                    returnRectangles.add(new Rectangle(blocker, new Point(blocker.getX() + width, blocker.getY() + height), blocker));
                } else {
                    useLeft = rand.nextBoolean();
                    if (useLeft) {
                        returnRectangles.add(new Rectangle(new Point(blocker.getX() - width, blocker.getY()), new Point(blocker.getX(), blocker.getY() + height), blocker));
                    } else {
                        returnRectangles.add(new Rectangle(new Point(blocker.getX(), blocker.getY()), new Point(blocker.getX() + width, blocker.getY() + height), blocker));
                    }
                }
            }
        }

        return toArray(returnRectangles);

    }

    Strategy2pos(TestData data) {
        this.data = data;
        this.height = data.result;
        this.width = data.ratio * data.result;
    }
}
