# Fitness over time
set term png
unset key
set output "FitnessOverTime.png"
set title 'fitness over time (p=20,sam=5,offline robot, offline state)'
set ylabel 'fitness'
set xlabel 'generation'

set style data lines
set style line 1 lt 2 lc rgb "grey" lw 1


plot '_fitness.log' u 1:2:3 w filledcu fs pattern 3 ls 1,'_fitness.log' u 1:2:4 w filledcu fs pattern 3 ls 1, \
'_fitness.log' u 1:3 with lines lt -1
      