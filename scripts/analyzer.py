import subprocess
import sys
import matplotlib.pylab as plt
import time
import os
# if you want to run the analyzer
# only change this directory and it should work
# for windows people (puke) slashes might need different syntax 
project_location = "/home/juris/Uni/DBL-Algorithms/"
test_location = project_location + "profiling/tests/" # location of generated tests
class_location = project_location + "out/production/DBL-Algorithms/" # location of compiled class files
output_location = project_location + "profiling/" # output graphs location 

os.chdir(class_location) # go to class files

# assuming first digit is nr Points
def nrPoints(filename):
    return [int(s) for s in filename.split('_') if s.isdigit()][0]

def runAlgo(algo_name):
    results = {} # npoints -> [totaltime, timesrun]

    total = sum(str(test).startswith(algo_name) for test in os.listdir(test_location))
    progress = 0;

    for test in os.listdir(test_location): # for each test
        if not str(test).startswith(algo_name):
           continue 

        main_command = ["java", "main.MainWrapper"] # this will run MainWrapper
        main_command.append(test_location + test) # invoke the test

        cur_time = time.time()
        subprocess.check_output(main_command) # run algo
        running_time = time.time() - cur_time
        # update results
        n = nrPoints(str(test)) # get nr of points and update results dictionary
        if n not in results:
            results[n] = [0, 0]
        results[n][0] += running_time
        results[n][1] += 1
        progress += 1
        time.sleep(0.1)
        sys.stdout.write(f"\r{progress}/{total}")
        sys.stdout.flush()

    return results

def analyze(algo_name, output_name):
    # run algo on tests
    pure_results = runAlgo(algo_name)
    results = [] 
    # process data
    for k, r in pure_results.items():
        results.append((k, round(r[0] / r[1], 2)))  # get avg
    results.sort()
    # plot it
    x, y = zip(*results)
    plt.plot(x, y) 
    plt.xlabel("|P|")
    plt.ylabel("t")
    plt.savefig(f"{output_location}{output_name}.png") 

analyze("2pos", "2pos_analysis")
