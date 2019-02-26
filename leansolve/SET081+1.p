%--------------------------------------------------------------------------
% File     : SET081+1 : TPTP v7.2.0. Bugfixed v5.4.0.
% Domain   : Set Theory
% Problem  : Only X can belong to {X}
% Version  : [Qua92] axioms : Reduced & Augmented > Complete.
% English  :

% Refs     : [Qua92] Quaife (1992), Automated Deduction in von Neumann-Bern
%          : [BL+86] Boyer et al. (1986), Set Theory in First-Order Logic:
% Source   : [Qua92]
% Names    :

% Status   : Theorem
% Rating   : 0.17 v7.2.0, 0.14 v7.1.0, 0.09 v7.0.0, 0.10 v6.4.0, 0.12 v6.3.0, 0.17 v6.2.0, 0.24 v6.1.0, 0.20 v6.0.0, 0.22 v5.4.0
% Syntax   : Number of formulae    :   44 (  16 unit)
%            Number of atoms       :  102 (  20 equality)
%            Maximal formula depth :    7 (   4 average)
%            Number of connectives :   63 (   5   ~;   3   |;  26   &)
%                                         (  19 <=>;  10  =>;   0  <=;   0 <~>)
%                                         (   0  ~|;   0  ~&)
%            Number of predicates  :    6 (   0 propositional; 1-2 arity)
%            Number of functors    :   26 (   5 constant; 0-3 arity)
%            Number of variables   :   88 (   0 sgn;  83   !;   5   ?)
%            Maximal term depth    :    4 (   1 average)
% SPC      : FOF_THM_RFO_SEQ

% Comments :
% Bugfixed : v5.4.0 - Bugfixes to SET005+0 axiom file.
%--------------------------------------------------------------------------
%----Include set theory axioms
include('Axioms/SET005+0.ax').
%--------------------------------------------------------------------------
%----SS3: Only X can belong to {X}
fof(only_member_in_singleton,conjecture,
    ( ! [X,Y] :
        ( member(Y,singleton(X))
       => Y = X ) )).

%--------------------------------------------------------------------------