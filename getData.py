import subprocess
import re
import sys
import pandas as pd


## Runs the java actor model using the paramaeters given in parameters and outputs the execution time
def run_cmd_and_get_time(N, alpha, TLE):
    print(f"Running test for values - N: {N}, Alpha: {alpha}, TLE: {TLE}")
    cmd = f'mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath com.example.synod.Main {N} {alpha} {TLE}"'
    try:
        output = subprocess.check_output(cmd, shell=True, stderr=subprocess.STDOUT, universal_newlines=True)
        # Use regular expression to find the time to decide
        match = re.search(r'decided (true|false) in (\d+)ms', output)
        if match:
            time_to_decide = int(match.group(2))
            print(f"Time to decide: {time_to_decide}")
            return time_to_decide
        else:
            return None, None
    except subprocess.CalledProcessError as e:
        print("Error running command:", e.output)
        return None, None

# Values for the test
N_values = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
alpha_values = [0,0.01, 0.1, 1]
TLE_values = [50, 100, 500, 1000]

if len(sys.argv) != 2:
    print("Usage: python script.py <file_name>")
    sys.exit(1)

# Reads the file name given as an input
file_name = sys.argv[1]

# Create a data frame to store the values
df = pd.DataFrame(columns=['N', 'Alpha', 'Tle', 'Execution Time'])
data_types = {'N': int, 'Alpha': float, 'Tle': int, 'Execution Time': int}

# Store the values to 

for N in N_values:
    for alpha in alpha_values:
        for TLE in TLE_values:
            time_to_decide = run_cmd_and_get_time(N, alpha, TLE)
            if time_to_decide is not None:
                df = df.append({'N': N, 'Alpha': alpha, 'Tle': TLE, 'Execution Time': time_to_decide}, ignore_index=True)
            else:
                print("Could not extract decision and time to decide.")

# Apply data types to DataFrame
df = df.astype(data_types)

# Save the data frame to a csv file
df.to_csv(file_name, index=False)