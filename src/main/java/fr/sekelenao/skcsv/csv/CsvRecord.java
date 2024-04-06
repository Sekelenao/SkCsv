package fr.sekelenao.skcsv.csv;

import java.util.ArrayList;

public interface CsvRecord {

    default Iterable<String> recordValues() {
        var values = new ArrayList<String>();
        var components = this.getClass().getRecordComponents();
        for(var component: components){
            if(component.isAnnotationPresent(CsvColumn.class)){
                var obj = SkRecordCsv.secureInvoke(component.getAccessor(), this);
                var value = obj == null ? "" : String.valueOf(obj);
                SkAssertions.conformValue(value);
                values.add(value);
            }
        }
        return values;
    }

}
