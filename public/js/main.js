function resizeChartByWindowSize() {
    $('#chart').css({ height: ($(window).height() - 20) + 'px' });
}

function buildSeriesByResponseData(data) {
    var series = [];
    var categories = [];

    $.each(data, function (index, weekday) {
        var points = [];

        $.each(weekday.entries, function (index, hourminute) {
            var hm = hourminute['hour-minute'];
            var category = hm.length === 3 ? '0' + hm : hm;

            if (categories.indexOf(category) === -1) {
                categories.push(category);
            }

            points.push(hourminute['avg-status']);
        });

        series.push({
            data: points,
            name: weekday.weekday
        });
    });

    return {
        series: series,
        categories: categories
    };
}

$(function () {
    resizeChartByWindowSize();
    $(window).on('resize', resizeChartByWindowSize);

    $.ajax('output.json', {
        success: function (data) {
            var seriesData = buildSeriesByResponseData(data);

            debugger;

            $('#chart').highcharts({
                chart: {
                    type: 'line'
                },
                title: {
                    text: 'Boulderwelt München Ost - Auslastung'
                },
                tooltip: {
                    shared: true
                },
                xAxis: {
                    categories: seriesData.categories
                },
                series: seriesData.series
            });
        }
    });
});
