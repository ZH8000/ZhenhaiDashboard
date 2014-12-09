package code.lib

case class Scale(domainMin: Double, domainMax: Double, rangeMin: Double, rangeMax: Double) {

  def apply(value: Double): Double = ((value - domainMin) * (rangeMax - rangeMin) / (domainMax - domainMin)) + rangeMin
  
}
