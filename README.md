# A skeleton code base to implement Synod (Paxos) in Akka.


## Todo:
- Do 5 times and take the average
- Get the mesures to a file for all values of tle, N and alpha
- After initialising  all instances, wait for the return message containing the time of execution. Then start the next one.
- Add a for loop to do this experiment 5 times and compute the average time.
- Print the data to a file


- Make the graphs python ? (see graphPlots.ipynb)
- Make a graph for fixed N and different tle 
- Make a graph for fixed Tle and different N
- Make a graph for fixed N and TLE with fixed alpha

- Do the data gathering on the same computer!!!!

to run the project, use the following command with the correct values of N alpha and TLE
mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath com.example.synod.Main N alpha TLE"



