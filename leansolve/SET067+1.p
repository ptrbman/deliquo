%------------------------------------------------------------------------------
% File     : SET067+1 : TPTP v7.2.0. Bugfixed v5.4.0.
% Domain   : Set Theory
% Problem  : If one argument is a proper class, pair contains only the other
% Version  : [Qua92] axioms : Reduced & Augmented > Complete.
% English  :

% Refs     : [Qua92] Quaife (1992), Automated Deduction in von Neumann-Bern
%          : [BL+86] Boyer et al. (1986), Set Theory in First-Order Logic:
% Source   : [Qua92]
% Names    :

% Status   : Theorem
% Rating   : 0.28 v7.2.0, 0.24 v7.1.0, 0.22 v7.0.0, 0.27 v6.4.0, 0.35 v6.3.0, 0.33 v6.2.0, 0.36 v6.1.0, 0.43 v6.0.0, 0.39 v5.5.0, 0.48 v5.4.0
% Syntax   : Number of formulae    :   44 (  17 unit)
%            Number of atoms       :  101 (  19 equality)
%            Maximal formula depth :    7 (   4 average)
%            Number of connectives :   62 (   5   ~;   3   |;  26   &)
%                                         (  19 <=>;   9  =>;   0  <=;   0 <~>)
%                                         (   0  ~|;   0  ~&)
%            Number of predicates  :    6 (   0 propositional; 1-2 arity)
%            Number of functors    :   26 (   5 constant; 0-3 arity)
%            Number of variables   :   88 (   0 sgn;  83   !;   5   ?)
%            Maximal term depth    :    4 (   1 average)
% SPC      : FOF_THM_RFO_SEQ

% Comments :
% Bugfixed : v5.4.0 - Bugfixes to SET005+0 axiom file.
%------------------------------------------------------------------------------
%----Include set theory axioms
include('Axioms/SET005+0.ax').
%------------------------------------------------------------------------------
%----UP2: If one argument is a proper class, pair contains only the other
fof(pair_contains_other,conjecture,
    ( ! [X,Y] : subclass(unordered_pair(X,X),unordered_pair(X,Y)) )).

%------------------------------------------------------------------------------
