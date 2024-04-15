To run the project use:
mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath com.example.synod.Main N:int alpha:float TLE:int"

Replace N, alpa, Tle to the desired value. You can add a 4th parameter debug:boolean (default value false) to add verbose.
