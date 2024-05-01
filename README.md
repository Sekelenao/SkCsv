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

Now, we all wonder how many plants are inventoried in the departmental territory of Hauts-de-Seine.

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

