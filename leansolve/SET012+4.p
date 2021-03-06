%--------------------------------------------------------------------------
% File     : SET012+4 : TPTP v7.2.0. Released v2.2.0.
% Domain   : Set Theory (Naive)
% Problem  : Complement is an involution
% Version  : [Pas99] axioms.
% English  :

% Refs     : [Pas99] Pastre (1999), Email to G. Sutcliffe
% Source   : [Pas99]
% Names    :

% Status   : Theorem
% Rating   : 0.38 v7.2.0, 0.34 v7.1.0, 0.30 v7.0.0, 0.37 v6.4.0, 0.38 v6.3.0, 0.42 v6.2.0, 0.36 v6.1.0, 0.50 v6.0.0, 0.52 v5.5.0, 0.56 v5.4.0, 0.61 v5.3.0, 0.63 v5.2.0, 0.50 v5.1.0, 0.57 v5.0.0, 0.71 v4.1.0, 0.65 v4.0.1, 0.70 v4.0.0, 0.71 v3.7.0, 0.70 v3.5.0, 0.74 v3.4.0, 0.84 v3.3.0, 0.71 v3.2.0, 0.82 v3.1.0, 0.78 v2.7.0, 0.83 v2.6.0, 0.86 v2.5.0, 0.88 v2.4.0, 0.25 v2.3.0, 0.00 v2.2.1
% Syntax   : Number of formulae    :   12 (   1 unit)
%            Number of atoms       :   31 (   3 equality)
%            Maximal formula depth :    7 (   5 average)
%            Number of connectives :   21 (   2 ~  ;   2  |;   4  &)
%                                         (  10 <=>;   3 =>;   0 <=)
%                                         (   0 <~>;   0 ~|;   0 ~&)
%            Number of predicates  :    4 (   0 propositional; 2-2 arity)
%            Number of functors    :    9 (   1 constant; 0-2 arity)
%            Number of variables   :   30 (   0 singleton;  29 !;   1 ?)
%            Maximal term depth    :    3 (   1 average)
% SPC      : FOF_THM_RFO_SEQ

% Comments :
%--------------------------------------------------------------------------
%----Include set theory definitions
include('Axioms/SET006+0.ax').
%--------------------------------------------------------------------------
fof(thI23,conjecture,
    ( ! [A,E] :
        ( subset(A,E)
       => equal_set(difference(E,difference(E,A)),A) ) )).

%--------------------------------------------------------------------------
