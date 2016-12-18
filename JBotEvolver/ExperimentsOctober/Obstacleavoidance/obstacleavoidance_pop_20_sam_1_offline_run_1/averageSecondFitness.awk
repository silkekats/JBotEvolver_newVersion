BEGIN{
# File input: one file data folder
# File input layout: eval, fitnesses
# File output: mean folder
# File output layout: 
}
/^[0-9]/{
	for (x in nums){
		delete nums[x];
		}
    sum = 0;
    n = 0;
    time = $1;
    std_dev=0;
    dev = 0;

    for (i=2; i <= NF; i++)
    {
        nums[n++] = $(i);
        sum += $(i);
    } 

    mean = sum/n;
    for (num in nums) {
    dev += (nums[num] - mean)^2;
    }
    std_dev = sqrt(dev/(n-1));


	ci = 1.96*(std_dev/sqrt(n));
	ci_low = mean - ci;
	ci_high = mean +ci;
    
    print mean;

}
