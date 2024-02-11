package fr.sekelenao.skcsv.csv;


import fr.sekelenao.skcsv.exception.CsvParsingException;

import java.util.Objects;

final class CsvSplitter {

    private static final class RowBuffer {

        private final CsvRow row;

        private StringBuilder cell;

        public RowBuffer(){
            this.row = new CsvRow();
            this.cell = new StringBuilder();
        }

        public void appendToCurrentCell(char c){
            SkAssertions.validChar(c);
            cell.append(c);
        }

        public void appendCellToRow(){
            row.addLast(cell.toString());
            cell = new StringBuilder();
        }

        public boolean notEmpty(){
            return !cell.isEmpty();
        }

    }

    private enum QuoteState {ENCOUNTERED, IN, OUT}
    private final char quote;
    private final char delimiter;
    private QuoteState quoteState = QuoteState.OUT;

    public CsvSplitter(RowConfiguration configuration){
        Objects.requireNonNull(configuration);
        this.quote = configuration.quote();
        this.delimiter = configuration.delimiter();
    }

    private void treatDelimiter(RowBuffer buffer, String text){
        switch (quoteState){
            case OUT ->  buffer.appendCellToRow();
            case IN -> buffer.appendToCurrentCell(delimiter);
            case ENCOUNTERED -> throw new CsvParsingException(text); // Not reachable
        }
    }

    private void treatQuote(RowBuffer buffer, String text){
        switch (quoteState){
            case OUT -> {
                if(buffer.notEmpty()) throw new CsvParsingException(text);
                quoteState = QuoteState.IN;
            }
            case ENCOUNTERED -> {
                buffer.appendToCurrentCell(quote);
                quoteState = QuoteState.IN;
            }
            case IN -> quoteState = QuoteState.ENCOUNTERED;
        }
    }

    public CsvRow split(String text){
        Objects.requireNonNull(text);
        if(text.isEmpty()) return new CsvRow();
        quoteState = QuoteState.OUT;
        var chars = text.toCharArray();
        var buffer = new RowBuffer();
        for(char c : chars){
            SkAssertions.validChar(c);
            if(c != quote && quoteState == QuoteState.ENCOUNTERED) quoteState = QuoteState.OUT;
            if(c == quote) treatQuote(buffer, text);
            else if(c == delimiter) treatDelimiter(buffer, text);
            else buffer.appendToCurrentCell(c);
        }
        if(chars[chars.length - 1] == delimiter || buffer.notEmpty()) buffer.appendCellToRow();
        if(quoteState == QuoteState.IN) throw new CsvParsingException(text);
        return buffer.row;
    }

}
