''' 
	Script runs through critical sections and builds a large csv table for the critical section

'''

#!/usr/bin/python

import sys
import os
from subprocess import call
import threading

def main():

	searchDirectory = "/Users/michaelshah/Documents/sootDump/"

	directories = os.listdir(searchDirectory)

	for d in directories:

		if os.path.isdir(searchDirectory+d+'/'):
			SummaryFile = open(searchDirectory+d+"summary.csv",'w')
			totalLines = 0
			SummaryFile.write('Critical Section Name:,# of method calls in first level,total # of method calls,# of synchronized method calls in first level,total # of sync method calls,Growth from first level,Number of allocations in first level,Total # of allocations,Growth from first level,total # lines of IR'+'\n')

			print '================================================='
			print '======vvvv======Bench marks:'+d+'======vvvv========='
			print '================================================='
			# Jump into the benchmark directory, and then explore each of the Critical Sections for .csv files
			if os.path.isdir(searchDirectory+d+'/CriticalSections/'):
				userfiles = os.listdir(searchDirectory+d+'/CriticalSections/')
				for f in userfiles:
					print '\t'+f
					if os.path.isdir(searchDirectory+d+'/CriticalSections/'+f):
						criticalSectionSummary = os.listdir(searchDirectory+d+'/CriticalSections/'+f)
						for summary in criticalSectionSummary:	
							if summary.endswith(".csv"):
								print '\t\t'+summary
								

								# Open file one line at a time
								with open(searchDirectory+d+'/CriticalSections/'+f+'/'+summary, 'r') as f:
								    for line in f:
								        SummaryFile.write(line+'\n')
								        totalLines = totalLines + 1
							    								# Open up the summary file for the single critical section
								#with open(searchDirectory+d+'/CriticalSections/'+f+'/'+summary, 'r') as content_file:
								#	content = content_file.read()
								#	SummaryFile.write(content)
								#	SummaryFile.write('\n')
								#call(["cat", searchDirectory+d+'/CriticalSections/'+f+'/'+ summary])
								#print
				print '================================================='
				print '======^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^========='
				print '================================================='
				rowA = '-'
				rowB = '=SUM(B2:B'+str(totalLines)+')'
				rowC = '=SUM(C2:C'+str(totalLines)+')'
				rowD = '=SUM(D2:D'+str(totalLines)+')'
				rowE = '=SUM(E2:E'+str(totalLines)+')'
				rowF = '=AVERAGE(F2:F'+str(totalLines)+')'
				rowG = '=SUM(G2:G'+str(totalLines)+')'
				rowH = '=SUM(H2:H'+str(totalLines)+')'
				rowI = '=AVERAGE(I2:I'+str(totalLines)+')'
				rowJ = '=SUM(J2:J'+str(totalLines)+')'
				SummaryFile.write(rowA+','+rowB+','+rowC+','+rowD+','+rowE+','+rowF+','+rowG+','+rowH+','+rowI+','+rowJ)
				SummaryFile.close()


if __name__ == "__main__":
	main()