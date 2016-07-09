var groupName;
var groupLink;

function addGroup() {
    groupName = $('#group').val();
    groupLink = $('#vk_link').val();
    checkGroups(groupName, groupLink);
};


function checkGroups(groupName, groupLink) {
    $.ajax({
        url: 'https://api.vk.com/method/groups.search?q=' + groupName + '&access_token='+getCookie('access_token'),
        type: "GET",
        crossDomain: true,
        dataType: 'jsonp',
        success: retrieveGroup
    });
};
function retrieveGroup(data) {
    data.response.forEach(function(group) {
        if (group.name === groupName) {
            setTimeout(getGroupInfo(group.gid), 0);
        }
    });
};

function getGroupInfo(groupId) {
    $.ajax({
        url: 'https://api.vk.com/method/groups.getById?group_ids=' + groupId + '&fields=links'+
        '&access_token='+getCookie('access_token'),
        type: "GET",
        crossDomain: true,
        dataType: 'jsonp',
        success: processGroupInfo
    });
}

function processGroupInfo(data) {
    if (!data.response) {
        return;
    }
    var response = data.response[0];
    for (var counter in response.links) {
        var link = response.links[counter];
        if(link.url === groupLink) {
            $.ajax({
                url: '/group/add?group='+groupName+'&link='+groupLink+'&groupId='+response.gid,
                async: true,
                body: JSON.stringify(link),
                type: "GET"
            });
            break;
        }
    }

};

function getCookie(name) {
    var matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

setInterval(function() {
    $('#tree').tree({
        url: '/group/getData'
    })
}, 10000);