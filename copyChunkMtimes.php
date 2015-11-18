<?php
// copy all the mcr headers in a folder into the matching mca files
// because the convertor looses valuable mtime data and the headers
// are compatible

$folder = $argv[1];
if(! is_dir($folder) ) {
    die("Error paramter one should be a directory");
}

$files = glob($argv[1] . '/*.{mcr}', GLOB_BRACE);
$prog = 0;
$total = count($files);
foreach($files as $file) {
    $region = pathinfo($file, PATHINFO_FILENAME);
    $dir = pathinfo($file, PATHINFO_DIRNAME);
    if( file_exists($dir . "/" . $region . ".mca") ) {
	$mcr_data_f = file_get_contents($dir . "/" . $region . ".mcr");
	$mca_data_f = file_get_contents($dir . "/" . $region . ".mca");
	$mcr_mtimes = mb_substr($mcr_data_f, 4096, 4096, '8bit');
	$mca_header = mb_substr($mca_data_f, 0, 4096, '8bit');
	$mca_mtimes = mb_substr($mca_data_f, 4096, 4096, '8bit'); // debug
	$mca_data = mb_substr($mca_data_f, 8192, NULL, '8bit');
	$newfile = $mca_header . $mcr_mtimes . $mca_data;
	file_put_contents($dir . "/" . $region . ".mca", $newfile);
	$prog++;
	$perc = ($prog / $total) * 100;
	echo "\rCopying chuck mtimes " . round($perc) . "%";
    }
    
}
echo " - Done\n";

function testdata($data, $offset = 0) {
    for($start = $offset; $start < $offset+(4112 - 4096); $start=$start+4) {
	$ts = 0;

	$ts += (ord($data[$start + 0]) << 12);
	$ts += (ord($data[$start + 1]) << 08);
	$ts += (ord($data[$start + 2]) << 04);
	$ts += (ord($data[$start + 3]) << 00);
	echo $start . ":";
	echo ord($data[$start + 0]) .",". ord($data[$start + 1]) .",". ord($data[$start + 2]) .",". ord($data[$start + 3]) . " = ";
	echo $ts . PHP_EOL;
    }
    echo PHP_EOL;
}
