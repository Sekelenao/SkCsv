<p align="center">
  <img src="logoSK.svg" width="100" alt="logo">
</p>

<h1 align="center">
  SkCsv library for Java 17+ (PREVIEW)
</h1>

## Warning

Please note that the development of this library is still ongoing, and there are no assurances regarding potential
changes to its structure or functionality in the future.

## Description

SkCsv is a library designed for easy data manipulation, simplifying the import and export processes. You don't need to 
worry about special characters or other complexities during parsing. The focus has been directed towards prioritizing 
speed and security. The level of abstraction allows you to forget that you're working with a CSV format. 

## Features

### Import datas from a file

To import data from a file, you can use the static method from of the SkCsv class. This method has several signatures. If nothing is specified as an argument for the CSV configuration, the default configuration ```SkCsvConfig.SEMICOLON``` is used. As for the charset, ```Charset.defaultCharset()``` is used.

```java
SkCsv csv = SkCsv.from(Paths.get("template.csv"), SkCsvConfig.COMMA, StandardCharsets.UTF_8);
```

The returned CSV will be configured with the default configuration. The configuration provided as a parameter is only used for parsing.

### Create a CSV from scratch with Java

To create a CSV from scratch with Java, you simply need to instantiate an object of the ```SkCsv``` class. This object will contain ```SkCsvRow``` objects, which represent the rows of the file. Here's how to create a CSV containing two rows with the varargs constructor :

```java
SkCsv csv = new SkCsv(
    new SkCsvRow("Language", "Type", "Rate"),
    new SkCsvRow("Java", "POO", "10/10")
);
```

You can also create a SkCsv an ```Iterable``` of ```SkCsvRow``` or an empty SkCsv with the default constructor.

```java
var secondCsv = new SkCsv(csv);
```

```java
var thirdCsv = new SkCsv(Collections.singleton(new SkCsvRow()));
```

### Configure the delimiter and the quotes

To configure a CSV, you just need to use the ```SkCsvConfig``` class. You can instantiate this class yourself or use its constants ```SEMICOLON``` and ```COMMA```.

```java
csv.configure(SkCsvConfig.SEMICOLON);
```

```java
csv.configure(new SkCsvConfig('/', '\''));
```

This configuration will be used during export and conversion to a String.

## Examples

### Export records without modifying the previous code

Imagine we have a massive and critical banking application. We would like to export our data to a CSV, but because we're
wary of tampering with these precious records, we don't want to add any code inside or export sensitive data.

Therefore, we will add an annotation to designate the data that we want to export.

```java
public record BankAccount(@CsvColumn String bankName, @CsvColumn UUID uuid, @CsvColumn BigDecimal balance, int secretCode)
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
    public BankAccount next() { ... }

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
