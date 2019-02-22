package models;

import interfaces.models.PointInterface;

import java.util.ArrayList;

public class FourPositionPoint extends Point {

    private ArrayList<FourPositionLabel> candidates = new ArrayList<>();

    public FourPositionPoint(PointInterface point) {
        super(point.getX(), point.getY());
    }

    public void addCandidate(FourPositionLabel label) {
        candidates.add(label);
    }

    public void removeCandidate(FourPositionLabel label) {
        candidates.remove(label);
    }

    public ArrayList<FourPositionLabel> getCandidates() { return candidates; }
}