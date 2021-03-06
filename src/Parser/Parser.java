package Parser;
/*
 * @author = Jeroen Schols
 */

import Collections.KDTree;
import Collections.QuadTree;
import interfaces.AbstractCollectionInterface;
import interfaces.ParserInterface;
import interfaces.models.GeometryInterface;
import interfaces.models.LabelInterface;
import interfaces.models.PointInterface;
import models.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Parser implements ParserInterface {

    private boolean testMode = false;
    private double optHeight, reqHeight;

    @Override
    public DataRecord input(InputStream source) throws NullPointerException, IOException {
        if (source == null) throw new NullPointerException("parser.input: source not found");

        DataRecord rec = new DataRecord();
        Scanner sc = new Scanner(source).useLocale(Locale.ENGLISH);

        if (!sc.hasNext()) {
            throw new IllegalArgumentException("Parser.input.pre violated: source length is zero");
        }

        try {
            while (!sc.hasNext("2pos|4pos|1slider")) sc.next();
            rec.placementModel = PlacementModelEnum.fromString(sc.next("2pos|4pos|1slider"));
        } catch (NoSuchElementException e) {
            throw new IOException("parser.input: no placement model found");
        }

        try {
            while (!sc.hasNextDouble()) sc.next();
            rec.aspectRatioString = sc.next();
            rec.aspectRatio = Double.parseDouble(rec.aspectRatioString);
        } catch (NoSuchElementException e) {
            throw new IOException("parser.input: no aspect ratio found");
        }

        int xMin = 10001;
        int yMin = 10001;
        int xMax = -1;
        int yMax = -1;

        List<PointInterface> points = new ArrayList<>();

        try {
            while (!sc.hasNextInt()) sc.next();
            int n = sc.nextInt();
            rec.labels = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                int x = sc.nextInt();
                int y = sc.nextInt();
                if (x < 0 || x > 10000 || y < 0 || y > 10000) {
                    throw new InputMismatchException("parser.input coordinates not in range {0,1,...,10000}");
                }

                xMin = Math.min(xMin, x);
                yMin = Math.min(yMin, y);
                xMax = Math.max(xMax, x);
                yMax = Math.max(yMax, y);

                LabelInterface label = null;
                points.add(new Point(x,y));

                switch (rec.placementModel) {
                    case TWO_POS:
                        label = new PositionLabel(x, y, 0, rec.aspectRatio, i, DirectionEnum.NE);
                        break;
                    case FOUR_POS:
                        label = new FourPositionLabel(x, y, 0, rec.aspectRatio, i, DirectionEnum.NE);
                        break;
                    case ONE_SLIDER:
                        label = new SliderLabel(x, y, 0, rec.aspectRatio, i);
//                        label = new SliderLabel(x, y, 0, rec.aspectRatio, 0, i);
                        break;
                }

                rec.labels.add(label);
            }
        } catch (NoSuchElementException e) {
            throw new IOException("parser.input: number of labels does not correspond to found coordinates");
        }

        rec.xMin = xMin;
        rec.xMax = xMax;

        rec.yMin = yMin;
        rec.yMax = yMax;

        rec.labels = Collections.unmodifiableList(rec.labels);
        switch (rec.placementModel) {
            case TWO_POS:
                rec.collection = initKDTree(rec.labels);
                break;
            case FOUR_POS:
                rec.collection = initKDTree4Pos(points);
                break;
            case ONE_SLIDER:
                rec.collection = initQuadTree(rec.labels, xMin, xMax, yMin, yMax);
        }

        // when in test-mode, the input file contains a
        if (testMode) {
            while (!sc.hasNextDouble()) sc.next();
            optHeight = sc.nextDouble();
            while (!sc.hasNextDouble()) sc.next();
            reqHeight = sc.nextDouble();
        }

        sc.close();
        return rec;
    }

    private QuadTree initQuadTree(Collection<LabelInterface> points, double xMin, double xMax, double yMin, double yMax) {
        return new QuadTree(points);
    }

    private KDTree initKDTree(List<LabelInterface> labels) {
        return new KDTree(new ArrayList<>(labels), 5);
    }

    private KDTree initKDTree4Pos(List<PointInterface> points) {
        KDTree tree = new KDTree(points, 1);

        return tree;
    }

    /**
     * Parse a test input to program structure retrieving a parsed DataRecord and the optimal height value.
     *
     * @param source          {@link Readable}
     * @return {@link TestDataRecord}
     * @throws NullPointerException if {@code source == null}
     * @throws IOException          if read error occurs
     */
    public TestDataRecord inputTestMode(InputStream source) throws IOException {
        testMode = true;
        DataRecord rec = input(source);
        testMode = false;
        return new TestDataRecord(rec, optHeight, reqHeight);
    }

    @Override
    public void output(DataRecord record, OutputStream stream) throws NullPointerException {
        if (record == null) throw new NullPointerException("parser.output: record not found");

        PrintWriter writer = new PrintWriter(stream);

        switch (record.placementModel) {
            case TWO_POS:
                writer.print("placement model: 2pos\n");
                break;
            case FOUR_POS:
                writer.print("placement model: 4pos\n");
                break;
            case ONE_SLIDER:
                writer.print("placement model: 1slider\n");
                break;
            default:
                throw new NoSuchElementException("parser.output placement model unknown");
        }

        DecimalFormat format = new DecimalFormat("0.0000000000000000000000000000");
        format.setRoundingMode(RoundingMode.FLOOR);

        writer.write(
                "aspect ratio: " + record.aspectRatioString + "\n"
                        + "number of points: " + record.labels.size() + "\n"
                        + "height: " + format.format(record.height) + "\n"
        );

        for (LabelInterface label : record.labels) {
            writer.write(Math.round(label.getPOI().getXMin()) + " " + Math.round(label.getPOI().getYMin()) + " " + label.getPlacement() + "\n");
        }

        writer.flush();
    }
}
