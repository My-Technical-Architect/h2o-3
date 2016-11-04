package water.udf;

import water.fvec.Vec;

/**
 * This column depends on two other columns
 */
public class Fun2Column<X, Y, Z> implements Column<Z> {
  private final Function2<X, Y, Z> f;
  private final Column<X> xs;
  private final Column<Y> ys;
  
  @Override public Vec vec() { return new VirtualVec(this); }

  @Override public int rowLayout() { return xs.rowLayout(); }

  public Fun2Column(Function2<X, Y, Z> f, Column<X> xs, Column<Y> ys) {
    this.f = f;
    this.xs = xs;
    this.ys = ys;
  }
  
  @Override public Z get(long idx) { 
    return isNA(idx) ? null : f.apply(xs.get(idx), ys.get(idx)); 
  }

  @Override
  public TypedChunk<Z> chunkAt(int i) {
    return new FunChunk(xs.chunkAt(i), ys.chunkAt(i));
  }

  @Override public boolean isNA(long idx) { return xs.isNA(idx) || ys.isNA(idx); }

  @Override
  public String getString(long idx) { return isNA(idx) ? "(N/A)" : String.valueOf(get(idx)); }

  /**
   * Pretends to be a chunk of a column, for distributed calculations.
   * Has type, and is not materialized
   */
  public class FunChunk implements TypedChunk<Z> {
    private final TypedChunk<X> cx;
    private final TypedChunk<Y> cy;

    public FunChunk(TypedChunk<X> cx, TypedChunk<Y> cy) {
      this.cx = cx;
      this.cy = cy;
    }

    @Override public Z get(int i) {
      return f.apply(cx.get(i), cy.get(i));
    }
  }

}
