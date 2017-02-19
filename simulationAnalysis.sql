select repository, goal, qtd_identified_sws, qtd_selected_sws from simulation where selected_sws is not null and repository = 'Repository1'  order by repository, goal;

select repository, goal, qtd_identified_sws, qtd_selected_sws from simulation where repository = 'Repository1'  order by repository, goal;

select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	(
	goal ilike 'Goal6_'
	or
	goal = 'Goal70')
	and 
	goal <> 'Goal60'
order by
	repository, goal; 


/*** Group One ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal_'
	or 
	goal = 'Goal10' )
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	goal ilike 'Goal_'
	or 
	goal = 'Goal10'
group by
	repository
order by 
	repository;

/*** Group Two ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal1_'
	or
	goal = 'Goal20')
	and 
	goal <> 'Goal10'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal1_'
	or
	goal = 'Goal20')
	and 
	goal <> 'Goal10'
group by
	repository
order by 
	repository;

/*** Group Three ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal2_'
	or
	goal = 'Goal30')
	and 
	goal <> 'Goal20'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal2_'
	or
	goal = 'Goal10')
	and 
	goal <> 'Goal20'
group by
	repository
order by 
	repository;

/*** Group Four ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal3_'
	or
	goal = 'Goal40')
	and 
	goal <> 'Goal30'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal3_'
	or
	goal = 'Goal20')
	and 
	goal <> 'Goal30'
group by
	repository
order by 
	repository;

/*** Group Five ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal4_'
	or
	goal = 'Goal50')
	and 
	goal <> 'Goal40'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal4_'
	or
	goal = 'Goal30')
	and 
	goal <> 'Goal40'
group by
	repository
order by 
	repository;

/*** Group Six ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository1' 
	and 
	(
	goal ilike 'Goal5_'
	or
	goal = 'Goal60')
	and 
	goal <> 'Goal50'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal5_'
	or
	goal = 'Goal40')
	and 
	goal <> 'Goal50'
group by
	repository
order by 
	repository;

/*** Group Seven ***/
select 
	repository, 
	goal, 
	qtd_identified_sws, 
	qtd_selected_sws 
from simulation 
where 
	repository = 'Repository10' 
	and 
	(
	goal ilike 'Goal6_'
	or
	goal = 'Goal70')
	and 
	goal <> 'Goal60'
order by
	repository, goal;  

select 
	repository,
	sum(qtd_identified_sws) as qtd_id, 
	sum(qtd_selected_sws) as qtd_selec
from simulation 
where 
	(
	goal ilike 'Goal6_'
	or
	goal = 'Goal70')
	and 
	goal <> 'Goal60'
group by
	repository
order by 
	repository;

select * from simulation where repository = 'Repository10' and goal = 'Goal67';