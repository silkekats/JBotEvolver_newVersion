# Boxplot fitness at reproduction time

set term png
set output "FitnessOverTimeBox.png"

#remove legend
unset key

set ylabel 'fitness'
set xlabel 'generation'


set style data boxplot

plot '_fitnessdetail.log' using (1.0):4:(0):1 lt -1  

#set term aqua