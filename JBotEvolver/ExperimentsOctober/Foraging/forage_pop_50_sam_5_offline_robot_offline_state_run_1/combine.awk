BEGIN { N = 20 }

!/#/ { for (i = 1; i <= NF; i++) sum[i] += $i }

     NR % N == 0  { for (i = 1; i <= NF; i++)
                   {
                       
                       printf("%.6f%s", sum[i]/N, (i == NF) ? "\n" : " ")
                       
                       sum[i] = 0
                   }
                 }