package treasurehunt.search;

public abstract class SearchNode<T> implements Comparable<SearchNode>
{
    private T value;
    private T parent;
    private int cost;

    public SearchNode(T value, T parent, int cost)
    {
        this.value = value;
        this.parent = parent;
        this.cost = cost;
    }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public T getValue() { return value; }
    public T getParent() { return parent; }

    @Override
    public int compareTo(SearchNode node)
    {
        return cost - node.getCost();
    }
}
