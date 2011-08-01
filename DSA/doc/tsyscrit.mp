prologues:=3;
beginfig(0);
numeric u;
u = 1cm;
draw (0,2.3u)--(0,0)--(4u,0);
draw (0,1.91u)--(3u,1.91u);
draw (3u,1.91u)--(3u,0);
pickup pencircle scaled 1pt;
draw(0,0)
  for i=1 upto 16: ..(i/4, 2*(1 - 2.7182 ** (-i/4)))*u endfor;
label.lrt(btex $T_{rx} + \eta T_{atm} (1-e^{-\tau_\nu/\sin(el)})+(1-\eta)T_{amb}$ etex, (3, sqrt 3)*u);
label.bot(btex $x = \tau_\nu/\sin(el)$ etex, (2u,0));
label.lft(btex $T_{sys}$ etex, (0,u));
label.lft(btex $T_{sys}^{max}$ etex, (0,1.91u));
label.top(btex $x^{max}$ etex, (3.5u,0))
endfig;
end
