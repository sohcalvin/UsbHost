use strict;
my $WHAT=$ARGV[0];

my $timestamp = getYYMMDDHHMISS();

if(! defined($WHAT)){
	printHelp();
	exit;	
}



################################################
# 1) rtm.jar@web
################################################
if($WHAT eq "UsbTest"){
	my $passwd = getFtpPasswd('pi@berry');
	if( open (FTP, "| ftp -i -v -n")){
	print FTP <<ECMD;
	open berry
	user pi $passwd
	cd projects/apps/
	bin
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

sub getFtpPasswd{
	my $keyName = shift;
	my $ftpPasswdFile = '/c/Users/calvinsoh/.ssh/ftp.passwd';
	open(FTP_PASS, "< $ftpPasswdFile") or die "Unable to open '$ftpPasswdFile', $!";
	while(my $aline = <FTP_PASS>){
		chomp $aline;	
		my($key, $pass) = split(/=/,$aline);
		$key =~ s/^\s+|\s+$//g;
		$pass =~ s/^\s+|\s+$//g;
		if($key eq $keyName){
			return $pass;
		}
	} 
	close(FTP_PASS);
}

sub getYYMMDDHHMISS(){
	my ($sec,$min,$hour,$day,$mon,$year)=localtime(time); 
	$mon+=1;
	if (length ($mon) == 1)  {$mon  = '0'.$mon;}
	if (length ($day) == 1)  {$day  = '0'.$day;}
	if (length ($hour)== 1)  {$hour = '0'.$hour;}
	if (length ($min) == 1)  {$min  = '0'.$min;}
	if (length ($sec) == 1)  {$sec  = '0'.$sec;}
	$year+=1900;$year=substr($year,2,2);
	my $tmpDATE=$year.$mon.$day.$hour.$min.$sec;
	return $tmpDATE;
	
}

