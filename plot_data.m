% This script will make a plot of accuracy and norm of difference matrices for each timepoint.
function [data] = plot_data(file_name, typeoutput)
key = '';
%data = zeros(numpoints, numnodes);
valid = ['NormDiff', 'NormDiffNormal', 'Accuracy'];
if size(file_name) == 0
	file_name = 'C:\Users\Ashutosh\Documents\GitHub\DistributedMulticlassSolver\DistributedMulticlassSVMSolver\output.txt';
end
if isfloat(typeoutput)
	if !(round(typeoutput) == typeoutput) || !isscalar(typeoutput)
		%Second thing is size of the size of the stuff isn't the same (different number of dimensions)
		%Third thing is that it has to be [1 1] (If we didn't have it, any 2D matrixo could sneak by) 
		disp(sprintf('Type output not an integer! Value: %f\n', typeoutput))
		return
	end
	if typeoutput == 0
		key = 'NormDiff';
	elseif typeoutput == 1
		key = 'NormDiffNormal';
	else
		key = 'Accuracy';
	end
elseif ischar(typeoutput)
	key = typeoutput;
	if sum(strfind(valid, key)) == 0
		disp(sprintf('Invalid typeoutput specification: %s!', key))
		return
	end
else
	disp(sprintf('Unrecognized class type for typeoutput: %s!', class(typeoutput)))
	return
end
disp(key)
file = fopen(file_name);
line = fgetl(file);
index = 1;
while ischar(line)
	concatenated = sprintf('Node: %%d; Timepoint: %%d; %s: %%f', key);
    A = sscanf(line, concatenated)';
	if size(A) == [1 3]
		data(A(2) + 1, A(1) + 1) = A(3);
	end
    line = fgetl(file);
	index = index + 1;
end
for i = 1:size(data)(2)
	X = linspace(1, size(data)(1), size(data)(1));
	Y = data(X, i);
	figure;
	fig = plot(X,Y);
	title(sprintf('Norm Diff (Percent) Plot For Node %d', i))
	ylabel(sprintf("Frobenius Norm for Difference Matrix Percent at Node %d", i))
	xlabel("Number of Cycles in PeerSim")
	axis([0 size(data)(1) 0 1])
	h = gcf;
	filename = sprintf("C:\\Users\\Ashutosh\\Dropbox\\School\\Eleventh Grade\\Columbia Internship\\Data Analysis\\NormDiffPercent Plot for Node %d.jpg", i);
	saveas(h, sprintf("NormDiffPercentPlotForNode%d.jpg", i))
	%saveas(h, filename)
end