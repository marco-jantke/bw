function resizeChartByWindowSize() {
    $('#chart').css({ height: ($(window).height() - 20) + 'px' });
}

function buildSeriesByResponseData(data) {
    var series = [];

    $.each(data, function (index, weekday) {
        var points = [];

        $.each(weekday.entries, function (index, hourminute) {
            points.push([
                parseInt(hourminute['hour-minute'], 10),
                hourminute['avg-status']
            ]);
        });

        points.sort(function (a, b) {
            return a[0] - b[0];
        });

        series.push({
            data: points,
            name: weekday.weekday
        });
    });

    return series;
}

$(function () {
    resizeChartByWindowSize();
    $(window).on('resize', resizeChartByWindowSize);

    $.ajax('output.json', {
        success: function (data) {
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
                series: buildSeriesByResponseData(data)
            });
        }
    });
});
