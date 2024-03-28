# CS4218codebase
codebase for CS4218, 23/24 Sem2

## Testing

### Testing with Maven

Run Maven `clean` and `test` Lifecycle scripts in the Maven Tool Window.

### Testing from the command line

1. cd to `cs4218_shell_2024` (directory with `pom.xml` )
2. run `maven clean test` 


## Running the Shell

### Running from jar file

1. cd to `cs4218_shell_2024` (directory with `pom.xml` )
2. run `maven clean package`. Alternatively, run Maven `clean` and `package` Lifecycle scripts in the Maven Tool Window.
3. cd to `./target`
4. run `java -jar cs4218_shell_2024-1.0-SNAPSHOT.jar`

### Running from ShellImpl

1. Open `ShellImpl.java`.
2. run main method.

_Note: Running directly from the file may cause unintended issues due to differing systems_



## Troubleshooting

Due to changing pom.xml `testSourceDirectory`, you may face issues with testing.

For Windows, go to `File` > `Invalidate Caches` to restart and reindex project


