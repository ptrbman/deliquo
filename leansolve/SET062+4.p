%--------------------------------------------------------------------------
% File     : SET062+4 : TPTP v7.2.0. Released v2.2.0.
% Domain   : Set Theory (Naive)
% Problem  : The empty set is a subset of all sets
% Version  : [Pas99] axioms.
% English  :

% Refs     : [Pas99] Pastre (1999), Email to G. Sutcliffe
% Source   : [Pas99]
% Names    :

% Status   : Theorem
% Rating   : 0.07 v7.2.0, 0.03 v7.1.0, 0.00 v7.0.0, 0.03 v6.4.0, 0.08 v6.3.0, 0.12 v6.2.0, 0.08 v6.1.0, 0.10 v6.0.0, 0.13 v5.5.0, 0.07 v5.4.0, 0.04 v5.3.0, 0.11 v5.2.0, 0.05 v5.0.0, 0.04 v4.0.0, 0.08 v3.7.0, 0.10 v3.5.0, 0.11 v3.4.0, 0.00 v3.2.0, 0.09 v3.1.0, 0.00 v2.2.1
% Syntax   : Number of formulae    :   12 (   2 unit)
%            Number of atoms       :   30 (   3 equality)
%            Maximal formula depth :    7 (   5 average)
%            Number of connectives :   20 (   2 ~  ;   2  |;   4  &)
%                                         (  10 <=>;   2 =>;   0 <=)
%                                         (   0 <~>;   0 ~|;   0 ~&)
%            Number of predicates  :    4 (   0 propositional; 2-2 arity)
%            Number of functors    :    9 (   1 constant; 0-2 arity)
%            Number of variables   :   29 (   0 singleton;  28 !;   1 ?)
%            Maximal term depth    :    2 (   1 average)
% SPC      : FOF_THM_RFO_SEQ

% Comments :
%--------------------------------------------------------------------------
%----Include set theory definitions
include('Axioms/SET006+0.ax').
%--------------------------------------------------------------------------
fof(thI15,conjecture,
    ( ! [A] : subset(empty_set,A) )).

%--------------------------------------------------------------------------
