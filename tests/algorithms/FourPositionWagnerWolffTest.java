package algorithms;

import Collections.KDTree;
import Parser.DataRecord;
import interfaces.models.LabelInterface;
import models.DirectionEnum;
import models.FourPositionLabel;
import models.FourPositionPoint;
import models.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FourPositionWagnerWolffTest {

    FourPositionWagnerWolff algo;

    @BeforeEach
    void setup() {
        algo = new FourPositionWagnerWolff();
    }

    @Test
    void findConflictSizes() {
    }

    @Test
    void preprocessing1() {
        DataRecord record = new DataRecord();
        record.aspectRatio = 1;
        record.collection = new KDTree();
        record.labels = new ArrayList<>();
        FourPositionPoint point1 = new FourPositionPoint(new Point(100, 100));
        FourPositionPoint point2 = new FourPositionPoint(new Point(120, 120));
        record.collection.insert(point1);
        record.collection.insert(point2);
        LabelInterface label1 = new FourPositionLabel(100, 100, 0, record.aspectRatio, 0, point1, DirectionEnum.NE);
        LabelInterface label2 = new FourPositionLabel(120, 120, 0, record.aspectRatio, 0, point1, DirectionEnum.NE);
        record.labels.add(label1);
        record.labels.add(label2);
        double sigma = 30;
        algo.preprocessing(record, sigma);
        assertTrue(algo.getPointsQueue().size() == 2);
        assertTrue(algo.getLabelsWithConflicts().size() == 4);
        assertTrue(algo.getSelectedLabels().size() == 0);
        assertTrue(algo.getLabels().collection.size() == 6);
    }

    @Test
    void preprocessing2() {
        DataRecord record = new DataRecord();
        record.collection = new KDTree();
        Point point1 = new Point(100, 100);
        Point point2 = new Point(140, 140);
        record.collection.insert(point1);
        record.collection.insert(point2);
        double sigma = 30;
        algo.preprocessing(record, sigma);
        assertTrue(algo.getPointsQueue().size() == 2);
        assertTrue(algo.getLabelsWithConflicts().size() == 2);
        assertTrue(algo.getSelectedLabels().size() == 0);
        assertTrue(algo.getLabels().collection.size() == 8);
    }

    @Test
    void createConflictGraph() {
    }

    @Test
    void eliminateImpossibleCandidates() {
    }

    @Test
    void applyHeuristic() {
    }

    @Test
    void doTwoSat() {
    }

    @Test
    void isSolvable() {
    }

    @Test
    void getSolution() {
    }
}