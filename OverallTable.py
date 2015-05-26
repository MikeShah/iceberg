#!/usr/bin/python

import csv
import os
from subprocess import call
import threading

def main():

    searchDirectory = "/Users/michaelshah/Documents/sootDump/"
    directories = os.listdir(searchDirectory)

    OverallTable = open(searchDirectory+"BigTable.csv",'w')
    
    for f in directories:
        if f.contains('.csv'):
            with open(f, 'rb') as csvfile:
                singleBenchmark = csv.reader(csvfile, delimiter=',', quotechar='|')
                for row in singleBenchmark:
                    print ', '.join(row)
        

    
        
        
