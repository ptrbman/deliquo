%--------------------------------------------------------------------------
% File     : SET024+1 : TPTP v7.2.0. Bugfixed v5.4.0.
% Domain   : Set Theory
% Problem  : A set belongs to its singleton
% Version  : [Qua92] axioms : Reduced & Augmented > Complete.
% English  :

% Refs     : [Qua92] Quaife (1992), Automated Deduction in von Neumann-Bern
%          : [BL+86] Boyer et al. (1986), Set Theory in First-Order Logic:
% Source   : [Qua92]
% Names    :

% Status   : Theorem
% Rating   : 0.07 v7.1.0, 0.04 v7.0.0, 0.07 v6.4.0, 0.08 v6.3.0, 0.12 v6.2.0, 0.24 v6.1.0, 0.20 v6.0.0, 0.13 v5.5.0, 0.07 v5.4.0
% Syntax   : Number of formulae    :   44 (  16 unit)
%            Number of atoms       :  102 (  19 equality)
%            Maximal formula depth :    7 (   4 average)
%            Number of connectives :   63 (   5   ~;   3   |;  26   &)
%                                         (  19 <=>;  10  =>;   0  <=;   0 <~>)
%                                         (   0  ~|;   0  ~&)
%            Number of predicates  :    6 (   0 propositional; 1-2 arity)
%            Number of functors    :   26 (   5 constant; 0-3 arity)
%            Number of variables   :   87 (   0 sgn;  82   !;   5   ?)
%            Maximal term depth    :    4 (   1 average)
% SPC      : FOF_THM_RFO_SEQ

% Comments :
% Bugfixed : v5.4.0 - Bugfixes to SET005+0 axiom file.
%--------------------------------------------------------------------------
%----Include set theory axioms
include('Axioms/SET005+0.ax').
%--------------------------------------------------------------------------
%----SS2: A set belongs to its singleton
fof(set_in_its_singleton,conjecture,
    ( ! [X] :
        ( member(X,universal_class)
       => member(X,singleton(X)) ) )).

%--------------------------------------------------------------------------
