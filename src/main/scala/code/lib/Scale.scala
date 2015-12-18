package code.lib

/**
 *  用內插法來將 domainMin 到 domainMax 中的某個值，對應到 rangeMin 到 rangeMax 中
 *  相對應的值。
 *
 *  @param    domainMin       原本的值域中的最小值
 *  @param    domainMax       原本的值域中的最大值
 *  @param    rangeMin        對應到的值域中的最小值
 *  @param    rangeMax        對應到的值域中的最大值
 */
case class Scale(domainMin: Double, domainMax: Double, rangeMin: Double, rangeMax: Double) {

  /**
   *  用內插法計算 value 位於對應到的值域中的值
   *
   *  @param    value     原本值域中的值
   *  @return             對應到新值域中的值
   */
  def apply(value: Double): Double = ((value - domainMin) * (rangeMax - rangeMin) / (domainMax - domainMin)) + rangeMin
  
}
