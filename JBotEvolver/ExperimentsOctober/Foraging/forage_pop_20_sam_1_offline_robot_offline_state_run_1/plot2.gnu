# Fitness over time
set term png
unset key
set output "SecondFitnessOverTime.png"
set title 'second fitness over time (p=20,sam=1,offline robot, offline state)'
set ylabel 'fitness'
set xlabel 'generation'
set yrange [0:2]

set style data lines
set style line 1 lt 2 lc rgb "grey" lw 1


plot 'secondFitness.txt' u 1:4 with lines lt -1