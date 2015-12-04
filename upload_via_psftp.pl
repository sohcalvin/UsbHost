
$WHAT=$ARGV[0];

$timestamp = getYYMMDDHHMISS();

if(! defined($WHAT)){
	printHelp();
	exit;	
}



################################################
# 1) rtm.jar@web
################################################
if($WHAT eq "UsbTest"){
	if( open (FTP, '| psftp -i ~/.ssh/psftp_rsa pi@berry')){
	print FTP <<ECMD;
	cd projects/apps/
	#bin
	put ./target/UsbTest.jar
	bye
ECMD
	close FTP;
}else {
	print "ERROR : Unable to open pipe to ftp for $WHAT\n";	
}
exit;
}

print "WARNING : Unregconized argument = $WHAT \n";
printHelp();


sub printHelp(){
	print <<EEE;
	Usage : perl upload.pl UsbTest
EEE
}


sub getYYMMDDHHMISS(){
	($sec,$min,$hour,$day,$mon,$year)=localtime(time); 
	$mon+=1;
	if (length ($mon) == 1)  {$mon  = '0'.$mon;}
	if (length ($day) == 1)  {$day  = '0'.$day;}
	if (length ($hour)== 1)  {$hour = '0'.$hour;}
	if (length ($min) == 1)  {$min  = '0'.$min;}
	if (length ($sec) == 1)  {$sec  = '0'.$sec;}
	$year+=1900;$year=substr($year,2,2);
	$tmpDATE=$year.$mon.$day.$hour.$min.$sec;
	return $tmpDATE;
	
}

