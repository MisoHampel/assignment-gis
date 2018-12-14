var opened = false;

function trigMenu() {
if(opened == false){
    openNav();
}   else{
    closeNav();
}
opened = !opened;
}

function openNav() {
    document.getElementById("mySidenav").style.width = "300px";
    document.getElementById("mySidenavMenu").style.marginLeft = "300px";
    document.getElementById("map").style.marginLeft = "340px";
}

function closeNav() {
    document.getElementById("mySidenav").style.width = "0";
    document.getElementById("mySidenavMenu").style.marginLeft = "0px";
    document.getElementById("map").style.marginLeft= "40px";
}