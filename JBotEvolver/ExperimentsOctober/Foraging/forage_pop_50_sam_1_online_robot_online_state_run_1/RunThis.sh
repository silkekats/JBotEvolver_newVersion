# script to run graphs for average fitness over lifetime group of robots

awk 'NF && $1!~/^#/' _fitnessdetail.log > _fitnessdetailNC.log
gawk -f combine.awk _fitnessdetailNC.log > secondFitness.txt



gnuplot <<EOF
load "plot.gnu"
load "plot2.gnu"
