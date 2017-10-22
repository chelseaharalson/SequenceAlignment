#!/usr/bin/perl

undef $/;
$_ = <>;
$n = 0;

for $match (split(/(?=>hsa)/)) {
      open(O, '>query' . ++$n . '.txt');
      print O $match;
      close(O);
}
