# script to run graphs for average fitness over lifetime group of robots

#gawk -f combine.awk _fitnessdetail.log


gnuplot <<EOF
#load "boxplot.gnu"
load "plot.gnu"
