/*
 * author = Jeroen Schols
 */

import Parser.DataRecord;
import interfaces.models.SquareInterface;

class Interpreter {


    static float getScore (DataRecord record) {
        if (isValid(record)) return record.height;
        return 0;
    }


    static boolean isValid (DataRecord record) {
        if (record.points == null || record.placementModel == null) return false;

        for (SquareInterface label : record.points) {
            if (record.labels.query2D(label).size() > 1) return false;
        }

        return true;
    }

}
