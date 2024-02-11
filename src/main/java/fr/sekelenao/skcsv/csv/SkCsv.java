package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class SkCsv {

    private final List<CsvRow> rows = new LinkedList<>();

    public SkCsv(CsvRow row){
        this.rows.add(row);
    }

    public void export(Path path) throws IOException {
        Files.write(path, rows.stream().map(CsvRow::toString).toList(), StandardCharsets.UTF_8);
    }

}
