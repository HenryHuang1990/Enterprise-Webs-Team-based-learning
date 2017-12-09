$(document).ready(function() {
	$.ajax({
		url : "TakeQuiz",
		type : "POST",
		dataType : "json",
		data : {
			task : "getActiveQuiz"
		},
		success : function(data) {
			console.log("out ready");
			var htmlData = getActiveQuizBody(data);
			$('.quizBody').html("");
			$('.quizResultBody').html("");
			$('.timerCountdown').hide();
			$('.quizDetails').html(htmlData);
		}
	});
});


function getActiveQuizBody(data) {
	var htmlData = "";
	htmlData += "<h4>Available Quizes: </h4>";
	//for each data.quiz
	var list = [1,2,3]; 
	$.each(list, function(index, value) {
		htmlData += "<div id='Quiz."+value+"' class='top-buffer form-horizontal'><div class='form-group'>" 
				+"		<label class='col-sm-2' >Quiz"+value+"</label>" 
				+		"<div class='col-sm-2'>" 
				+		"<input type='text' class='form-control' placeholder='Enter Token'></div>" 
				+		"<div class='col-sm-2'>" 
				+			"<button type='button' class='btn btn-link btn-sm startQuiz'>"
				+				"<span class='glyphicon glyphicon-new-window'></span> Start Quiz</button></div>"
				+"</div></div>";
	});
	
	return htmlData;
}


$(document).on("click",".startQuiz",  function(){
	console.log ('startQuiz');
	countdown("1:00");
	$('.timerCountdown').show();
	$('.quizDetails').html("");
	var options = [1,2,3,4];
	var quest = "What is 2+2?";
	var currQuest=1;
	var totalQuest=10;
	var htmlData = getQuestionBody(quest, options, currQuest, totalQuest);	
	$('.quizBody').html(htmlData);
	
});


$(document).on("click",".prevQuestionQuiz",  function(){
	console.log ('prevQuestionQuiz');
	$('.quizDetails').html("");
	var options = [1,2,3,4];
	var quest = "What is 2+2?";
	var currQuest=1;
	var totalQuest=10;
	var htmlData = getQuestionBody(quest, options, currQuest, totalQuest);	
	$('.quizBody').html(htmlData);
	
});


$(document).on("click",".nextQuestionQuiz",  function(){
	console.log ('nextQuestionQuiz');
	$('.quizDetails').html("");
	var options = ['St. Louis','Jefferson City','Kansas City','Springfield'];
	var quest = "What is captial of Missouri?";
	var currQuest=2;
	var totalQuest=10;
	var htmlData = getQuestionBody(quest, options, currQuest, totalQuest);	
	$('.quizBody').html(htmlData);
});


function getQuestionBody(quest, options, currQuest, totalQuest) {
	var htmlData = "";
	htmlData += "<div class='quizBodyHeader'>"
			+ "		<div class='questionNumber'> Question "+currQuest+" of "+totalQuest+"</div>"	
			+ "		<div class='question h4'>" + quest
			+ "		</div>"
			+ "		<div class='question_options'>";
	
	$.each(options, function(index,value) {
		htmlData += "<div class='radio'>"
			  + "		<label><input type='radio' name='option'>"+value+"</label>"
			  + "		</div>";
	});
	
	htmlData += "</div></div>";
	
	htmlData += "<div class='quizBodyFooter float-right'>"
			+"		<button type='button' class='btn btn-link btn-sm prevQuestionQuiz'>"
			+"			<span class='glyphicon glyphicon-triangle-left'></span> Previous Question</button>"
			+"		<button type='button' class='btn btn-link btn-sm nextQuestionQuiz'>Next Question "
			+"			<span class='glyphicon glyphicon-triangle-right'></span></button>"
			+"		<button type='button' class='btn btn-link btn-sm finishQuiz'>"
			+"			<span class='glyphicon glyphicon-off'></span> Finish Quiz</button></div>";
	
			return htmlData;
}


$(document).on("click",".finishQuiz",  function(){
	console.log ('finishQuiz');
	var ans = confirm("Finish test?");
	if(ans){
		finishClicked();
	}	
});

function finishClicked(){
	$('.quizBody').html("");
	var htmlData = "";
	$('.quizResultBody').html("quiz completed. results");
	$('.timerCountdown').hide();
	$('.countdown').html('');
}

function countdown(timer2) {
	var interval = setInterval(function() {
		var timer = timer2.split(':');
		var minutes = parseInt(timer[0], 10);
		var seconds = parseInt(timer[1], 10);
		--seconds;
		minutes = (seconds < 0) ? --minutes : minutes;
		if (minutes < 0) clearInterval(interval);
		seconds = (seconds < 0) ? 59 : seconds;
		seconds = (seconds < 10) ? '0' + seconds : seconds;
		$('.countdown').html(minutes + ':' + seconds);
		timer2 = minutes + ':' + seconds;
		if(minutes == -1){
			alert("Your time is up!!!");
			finishClicked();
		}
	}, 1000);
}