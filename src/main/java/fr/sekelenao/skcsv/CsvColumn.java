package fr.sekelenao.skcsv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Indicates that the annotated record component should be considered when exporting records to a CSV file via the
 *  {@link SkCsvRecords} class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT})
public @interface CsvColumn {}