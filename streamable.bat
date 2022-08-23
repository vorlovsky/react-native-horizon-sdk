@echo off
ffprobe  -v debug %1  2>&1 | findstr /c:"seeks"
rem ffmpeg -v trace -i %1 2>&1 | findstr /l "type:'moov' type:'mdat'"