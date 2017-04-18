public class TypeChecker {

    private int getLength(TreeNode s) {
        return s.isLeaf() ? 0 : 1 + getLength(s.getRight());
    }

    public Type getType(TreeNode s) {
        if (s.isLeaf()) {
            if(s.getToken().getType() == TokenType.NUMERIC) {
                return Type.NAT;
            } else if (s.getToken().getValue().equals(Constants.T)
                    || s.getToken().getValue().equals(Constants.F)) {
                return Type.BOOL;
            } else if (s.getToken().getValue().equals(Constants.NIL)) {
                return Type.LIST;
            } else {
                return Type.UNKNOWN;
            }
        }

        if (!s.getLeft().isLeaf()) {
            return Type.UNKNOWN;
        }

        String func = s.getLeft().getToken().getValue();

        switch (func) {
            case Constants.CAR:
                return getLength(s) == 2 && getType(s.getRight().getLeft()) == Type.LIST ? Type.NAT : Type.UNKNOWN;
            case Constants.CDR:
                return getLength(s) == 2 && getType(s.getRight().getLeft()) == Type.LIST ? Type.LIST : Type.UNKNOWN;
            case Constants.CONS:
                return getLength(s) == 3 && getType(s.getRight().getLeft()) == Type.NAT
                        && getType(s.getRight().getRight().getLeft()) == Type.LIST ? Type.LIST : Type.UNKNOWN;
            case Constants.ATOM:
            case Constants.INT:
                return getLength(s) == 2 && getType(s.getRight().getLeft()) != Type.UNKNOWN ? Type.BOOL : Type.UNKNOWN;
            case Constants.EQ:
                return getLength(s) == 3 && getType(s.getRight().getLeft()) == Type.NAT
                        && getType(s.getRight().getRight().getLeft()) == Type.NAT ? Type.BOOL : Type.UNKNOWN;
            case Constants.NULL:
                return getLength(s) == 2 && getType(s.getRight().getLeft()) == Type.LIST ? Type.BOOL : Type.UNKNOWN;
            case Constants.PLUS:
                return getLength(s) == 3 && getType(s.getRight().getLeft()) == Type.NAT
                        && getType(s.getRight().getRight().getLeft()) == Type.NAT ? Type.NAT : Type.UNKNOWN;
            case Constants.LESS:
                return getLength(s) == 3 && getType(s.getLeft().getRight()) == Type.NAT
                        && getType(s.getRight().getRight().getLeft()) == Type.NAT ? Type.BOOL : Type.UNKNOWN;
            case Constants.COND:
                if (getLength(s) < 2) {
                    return Type.UNKNOWN;
                }

                for (TreeNode t = s.getRight(); !t.isLeaf(); t = t.getRight()) {
                    if (t.getLeft().isLeaf() || getLength(t.getLeft()) != 2
                            || getType(t.getLeft().getLeft()) != Type.BOOL) {
                        return Type.UNKNOWN;
                    }
                }

                Type expType = getType(s.getRight().getLeft().getRight().getLeft());
                if (expType == Type.UNKNOWN) {
                    return Type.UNKNOWN;
                }

                for(TreeNode t = s.getRight(); !t.isLeaf(); t = t.getRight()) {
                    if (getType(t.getLeft().getRight().getLeft()) != expType) {
                        return Type.UNKNOWN;
                    }
                }

                return expType;
            default:
                return Type.UNKNOWN;
        }
    }

    public boolean isEmptyListSafe(TreeNode s) {
        if(s.isLeaf()) {
            return true;
        }

        String func = s.getLeft().getToken().getValue();

        switch (func) {
            case Constants.CAR:
            case Constants.CDR:
                return minLength(s.getRight().getLeft()) > 0;
            default:
                return true;
        }
    }

    private int minLength(TreeNode s) {
        if (s.isLeaf() && s.getToken().getValue().equals(Constants.NIL)) {
            return 0;
        }

        String func = s.getLeft().getToken().getValue();

        switch (func) {
            case Constants.CONS:
                return 1 + minLength(s.getRight().getRight().getLeft());
            case Constants.COND:
                int min = minLength(s.getRight().getLeft().getRight().getLeft());

                for(TreeNode t = s.getRight(); !t.isLeaf(); t = t.getRight()) {
                    int len = minLength(t.getLeft().getRight().getLeft());
                    if (len < min) {
                        min = len;
                    }
                }

                return min;
        }

        return -1;
    }
}
