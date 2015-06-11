#!/usr/bin/python

import csv
import os
import sys

# Special pyexcel library (use 'pip install pyexcel' to install)
from pyexcel.cookbook import merge_all_to_a_book
import pyexcel.ext.xlsx # Use pip install pyexcel-xlsx
import glob

def main():

	fileName= "_BigTable.csv"
	# Determine where we are dumping our files
	if len(sys.argv) <= 0:
		print "error, first argument should specify where to search for .csv files"
	searchDirectory = sys.argv[0] # "/Users/michaelshah/Documents/sootDump/"

    # Create a file where we will dump our output
	OverallTable = open(searchDirectory+fileName,'w')
    # Get a list of the directories that we are searching
	directories = os.listdir(searchDirectory)

	print ('==============================')
	print ('===Building Program Summary===')
	print ('==============================')

	# List all of the files we find
	for d in directories:
		fullpath = os.path.join(searchDirectory, d)
		if os.path.isfile(fullpath):
			if 'csv' in fullpath and fileName not in fullpath:
				# For each file we find, if it is a .csv file
				# Take the last row and compile it into a new row
				# in our spreadsheet.
				with open(fullpath, 'rb') as csvfile:
					csvReader = csv.reader(csvfile,delimiter=',', quotechar='|')
					# Search data and gather the last row
					counter = 0
					lastrow = ""
					for i in csvReader:
						counter=counter+1
						lastrow = i
					#print lastrow

					# Figure out how many columns there are which corresponds to number of commas
					column_count = str(lastrow).count(",")
					ExcelColumnNames = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','U','Z','AA','AB','AC','AD','AE']
					# External reference in our workout
					# Build a .csv style string that copies from each of the csv dumps into a single file
					replacementString = d+","
					for i in range(1,column_count+1):
						replacementString += "="+d+"!"+ExcelColumnNames[i]+str(counter)
						if i!=column_count:
							replacementString += ','

					# Make sure to convert all text to numbers
					#print replacementString
					OverallTable.write(replacementString+"\n")
	OverallTable.close()
	# Generate a workbook with all of the output.
	# This takes in all of our generated csv's and our new .csv that has an overall summary
	# and then converts it into one giant Excel document.
	merge_all_to_a_book(glob.glob(searchDirectory+"*.csv"),searchDirectory+"output.xlsx")
	print 'Program Summary Built'
	print 'Output: '+searchDirectory+"output.xlsx"

main()

    
        
        
