<p align="center">
  <img src="logo.png" width="150">
</p>

# SkCsv library for Java 17+ (PREVIEW)

## Warning

Please note that the development of this library is still ongoing, and there are no assurances regarding potential
changes to its structure or functionality in the future.

## Description

SkCsv is a library designed for easy data manipulation, simplifying the import and export processes. You don't need to 
worry about special characters or other complexities during parsing. The focus has been directed towards prioritizing 
speed and security. The level of abstraction allows you to forget that you're working with a CSV format. 

## Examples

### Explore a French plant dataset

For this initial example, we will utilize a dataset available at 
https://www.data.gouv.fr/fr/datasets/inventaire-de-la-flore/#/resources. For each line, excluding the first one 
(headers), we will have:

Scientific name; French name; Municipality; Identifier; Observation date.

```
Ranunculus bulbosus L.;Renoncule bulbeuse;VAUCRESSON;92076;2003-08-04
Ranunculus repens L.;Renoncule rampante;VAUCRESSON;92076;2003-08-04
Diplotaxis tenuifolia (L.) DC.;Roquette sauvage;VAUCRESSON;92076;2003-08-04
```

#### Data validation

It's noticeable that the dataset contains entries with 6 columns; thus, we will proceed to remove them. To do this, we
will import the file into our Java object. This object will handle everything; there's no need to worry about entries
containing the separator or quotes within values, as SkCsv manages this for us.

```java
public final class Main {

    private static final int EXPECTED_ROW_SIZE = 5;

    public static void main(String[] args) throws IOException {
        var csv = SkCsv.from(Paths.get("inventaire-de-la-flore.csv")).stream().skip(1)
                .filter(row -> row.size() == EXPECTED_ROW_SIZE)
                .collect(SkCsv.collector());
    }

}
```

#### Statistics

Now, we all wonder how many different plants are inventoried in the departmental territory of Hauts-de-Seine.

```java
public final class Main {

    private static final int EXPECTED_ROW_SIZE = 5;

    private static final int NAME_INDEX = 1;

    public static void main(String[] args) throws IOException {
        var plantNamesSet = new HashSet<String>();
        var csv = SkCsv.from(Paths.get("inventaire-de-la-flore.csv")).stream().skip(1)
                .filter(row -> row.size() == EXPECTED_ROW_SIZE)
                .collect(SkCsv.collector());
        csv.forEach(row -> plantNamesSet.add(row.get(NAME_INDEX)));
        System.out.println(plantNamesSet.size());
    }

}
```

#### Export

We would like to export this data to a CSV. To do this, we will use the export method of the SkCsv class. In the program below, we have added a second filter that will remove duplicates in the CSV itself.

```java
public final class Main {

    private static final int EXPECTED_ROW_SIZE = 5;

    private static final int NAME_INDEX = 1;

    public static void main(String[] args) throws IOException {
        var plantNamesSet = new HashSet<String>();
        var csv = SkCsv.from(Paths.get("inventaire-de-la-flore.csv")).stream().skip(1)
                .filter(row -> row.size() == EXPECTED_ROW_SIZE)
                .filter(row -> {
                    var name = row.get(NAME_INDEX);
                    if(!plantNamesSet.contains(name)) {
                        plantNamesSet.add(name);
                        return true;
                    }
                    return false;
                })
                .collect(SkCsv.collector());
        csv.export(Paths.get("out-skcsv-de-la-flore.csv"), StandardOpenOption.CREATE);
    }

}
```

#### Format export

Finally, we would like to keep only the plant names in the produced CSV. To do this, we will use the map function of the
stream API and recreate the lines by keeping only the indexes that interest us. Furthermore, to add a twist, we notice
that some plant names contain commas. However, we still want to export the CSV with commas as separators. Well, SkCsv
will handle that for us.

```java
public final class Main {

    private static final int EXPECTED_ROW_SIZE = 5;

    private static final int NAME_INDEX = 1;

    public static void main(String[] args) throws IOException {
        var plantNamesSet = new HashSet<String>();
        var csv = SkCsv.from(Paths.get("inventaire-de-la-flore.csv")).stream().skip(1)
                .filter(row -> row.size() == EXPECTED_ROW_SIZE)
                .filter(row -> {
                    var name = row.get(NAME_INDEX);
                    if(!plantNamesSet.contains(name)) {
                        plantNamesSet.add(name);
                        return true;
                    }
                    return false;
                })
                .map(row -> new SkCsvRow(row.get(0), row.get(1)))
                .collect(SkCsv.collector());
        csv.configure(SkCsvConfig.COMMA).export(Paths.get("out-skcsv-de-la-flore.csv"), StandardOpenOption.CREATE);
    }

}
```

### Export records without modifying the previous code

Imagine we have a massive and critical banking application. We would like to export our data to a CSV, but because we're
wary of tampering with these precious records, we don't want to add any code inside or export sensitive data.

Therefore, we will add an annotation to designate the data that we want to export.

```java
public record BankAccount(@CsvColumn String bankName, @CsvColumn UUID uuid, @CsvColumn BigDecimal balance, int secretCode){}
```

Let's imagine that within the application, we have a way to obtain these records either as an Iterable or as an Iterator,
as shown below.

```java
private static final Iterator<BankAccount> BANK_ACCOUNT_ITERATOR = new Iterator<>() {

    private int index;

    private static final Random RANDOM = new Random();

    @Override
    public boolean hasNext() {
        return index < 1_000_000;
    }

    @Override
    public BankAccount next() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
        return new BankAccount("OnlyBank", UUID.randomUUID(), BigDecimal.valueOf(Math.random() * 100), RANDOM.nextInt());
    }

};
```

In terms of lines of code, we'll only need a single line ! The parser will take care of everything as usual;
it uses the String.valueOf method for annotated types. Unlike the rest of the library, the absence of null values is
not guaranteed!

```java
public final class Main {
    
    public static void main(String[] args) throws IOException {
        SkCsvRecords.export(Paths.get("out.csv"), BANK_ACCOUNT_ITERATOR, SkCsvConfig.SEMICOLON);
    }

}
```

To obtain a huge CSV file of 1_000_000 records in ~2 seconds :

```
OnlyBank;a78e71e4-4c6f-4fde-990e-21274c2bd809;57.44878667185084
OnlyBank;2ebf4749-77e3-4158-85c6-5423c5f0b791;80.68069692551795
OnlyBank;89bd2b50-1aa5-4bb0-bf19-15defb47a0ed;51.924661497651535
```
