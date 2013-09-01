% This script will make a plot of accuracy and norm of difference matrices for each timepoint.
function [data] = testfork(file_name)
key = '';
%data = zeros(numpoints, numnodes);
if size(file_name) == 0
	file_name = 'C:\Users\Ashutosh\workspace\Multiclass SVM Primal Solver\out.txt';
end
file = fopen(file_name);
line = fgetl(file);
index = 1;
numforeach = sscanf(line, '%d')
line = fgetl(file);
small = zeros(numforeach, 1);
large = zeros(numforeach, 1);
while ischar(line)
	format = '%f';
    A = sscanf(line, format)';
	if index <= numforeach
		small(index, 1) = A;
	else
		large(index - numforeach, 1) = A;
	end
    line = fgetl(file);
	index = index + 1;
end
small;
large;
[p, t, d] = t_test_2(small, large, '<')
mean(small)
std(small)
mean(large)
std(large)