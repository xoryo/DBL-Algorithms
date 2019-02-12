package interfaces.models;

public interface SquareInterface {
    SquareInterface getBottomLeft();
    SquareInterface getBottomRight();
    SquareInterface getTopLeft();
    SquareInterface getTopRight();
    SquareInterface getCenter();
    SquareInterface getAnchor();
    void setAnchor(SquareInterface anchor);
    Float getWidth();
    Float getHeight();
    void setWidth(Float width);
    void setHeight(Float height);
    Float getAspectRatio();
    Boolean equals(SquareInterface square);
    Float getXMax();
    Float getYMax();
    Float getXMin();
    Float getYMin();
    Boolean intersect(SquareInterface square);
}