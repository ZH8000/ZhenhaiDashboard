function setupMonthChooser(selector, maxYear, maxMonth, minYear, minMonth, urlPrefix) {

  $(selector).monthpicker({
    pattern: 'yyyy-mm',
    startYear: minYear,
    finalYear: maxYear,
    selectedYear: minYear,
    monthNames: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  });

  $(selector).monthpicker().bind('monthpicker-change-year', function (e, year) {

    // Enable all months
    $(selector).monthpicker('disableMonths', []);

    // Disable month before our first log
    if (year == minYear) {
      var disableMonths = [];
      for (var i = 1; i < minMonth; i++) {
        disableMonths.push(i);
      }
      $(selector).monthpicker('disableMonths', disableMonths);
    }

    // Disable month after our last log
    if (year == maxYear) {
      var disableMonths = [];
      for (var i = (+maxMonth + 1); i <= 12; i++) {
        disableMonths.push(i);
      }
      $(selector).monthpicker('disableMonths', disableMonths);
    }
  });

  
  $(selector).bind('monthpicker-click-month', function(e, month) {
    var columns = $(selector).val().split("-");
    var year = +(columns[0]);
    var month = +(columns[1]);
    window.location = urlPrefix + "/" + year + "/" + month
  });

  function disableMonths() {
    var disableMonths = [];

    for (var i = 1; i < minMonth; i++) {
      disableMonths.push(i);
    }

    if (minYear == maxYear) {

      for (var i = +(maxMonth) + 1; i <= 12; i++) {
        disableMonths.push(i);
      }
    }

    $(selector).monthpicker('disableMonths', disableMonths);
  }

  disableMonths();

}

