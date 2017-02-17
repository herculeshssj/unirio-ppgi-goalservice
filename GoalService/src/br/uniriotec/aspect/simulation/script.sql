-- Database: simulation
-- Role: simulation
-- Schema: public

create role simulation login encrypted password 'simulation' valid until 'infinity';

create database simulation with owner simulation encoding = 'UTF8';

create table simulation (
	id serial,
	caseid varchar(20),
	repository varchar(20),
	goal varchar(10),
	identified_sws text,
	selected_sws text,
	qtd_identified_sws integer,
	qtd_selected_sws integer,
	running boolean
) ;

alter table public.simulation owner to simulation;