% This script will make a plot of accuracy and norm of difference matrices for each timepoint.
function [data] = plot_data_withpush(file_name, typeoutput)
key = '';
%data = zeros(numpoints, numnodes);
valid = ['NormDiff', 'NormDiffNormal', 'Accuracy'];
posofcurrentfile = strfind(mfilename('fullpath'), filesep)(size(strfind(mfilename('fullpath'), filesep))(1, 2));
directory = substr(mfilename('fullpath'), 1, posofcurrentfile);
str = sprintf('%s%s', directory, strcat('DistributedMulticlassSVMSolver', filesep, 'output.txt'))
if size(file_name) == 0
	file_name = str;
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
	concatenated = sprintf('Node: %%d; Time: %%d; Accuracy: %%f; Norm Diff Matrix: %%f; Norm Diff Percent: %%f');
    A = sscanf(line, concatenated)';
	if size(A) == [1 5]
		if strcmp(key, 'Accuracy')
			data(A(2) + 1, A(1) + 1) = A(3);
		elseif strcmp(key, 'NormDiff')
			data(A(2) + 1, A(1) + 1) = A(4);
		else
			data(A(2) + 1, A(1) + 1) = A(5);
		end
	else 
		metadata = sscanf(line, sprintf('Cycles: %%d; Nodes: %%d'));
		data = zeros(metadata(1, 1), metadata(2, 1));
	end
    line = fgetl(file);
	index = index + 1;
end
for i = 1:size(data)(2)
	X = linspace(1, size(data)(1), size(data)(1));
	Y = data(X, i);
	figure;
	fig = plot(X,Y, '*');
	title(sprintf('%s Plot For Node %d', key, i))
	ylabel(sprintf("Value at Node %d", i))
	xlabel("Number of Cycles in PeerSim")
	if strcmp(key, 'Accuracy')
		axis([0 size(data)(1) 0 1])
	elseif strcmp(key, 'NormDiff')
		axis([0 size(data)(1) 0 max(data) * 11/10])
	else
		axis([0 size(data)(1) 0 max(data) * 11/10])
	end
	h = gcf;
	filename = sprintf("C:\\Users\\Ashutosh\\Dropbox\\School\\Eleventh Grade\\Columbia Internship\\Data Analysis\\NormDiffPercent Plot for Node %d.jpg", i);
	saveas(h, sprintf("%sDotPlotForNode%d.jpg", key, i))
	%saveas(h, filename)
end