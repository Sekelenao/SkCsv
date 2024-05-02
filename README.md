# SkCsv library for Java 17+

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
stream API and recreate the lines by keeping only the indexes that interest us.

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